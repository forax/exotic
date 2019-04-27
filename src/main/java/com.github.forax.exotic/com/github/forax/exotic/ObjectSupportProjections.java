package com.github.forax.exotic;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.github.forax.exotic.ObjectSupport.ProjectionFunction;

class ObjectSupportProjections {
  interface ProjectionDeserializer {
    /**
     * @param lookup a lookup on the class containing the lambda.
     * @return the serialized lambda info of this lambda.
     */
    default SerializedLambda asSerializedLambda(Lookup lookup) {
      return extractSeralizedLambda(this, lookup);
    }
  }
  
  private static MethodHandle PRIVATE_LOOKUP_IN;
  static {
    Lookup lookup = publicLookup();
    MethodHandle privateLookupIn;
    try {
      privateLookupIn = lookup.findStatic(MethodHandles.class, "privateLookupIn", methodType(Lookup.class, Class.class, Lookup.class));
    } catch(IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (@SuppressWarnings("unused") NoSuchMethodException e) {
      // JDK 8
      privateLookupIn = null;
    } 
    PRIVATE_LOOKUP_IN = privateLookupIn;
  }
  
  static SerializedLambda extractSeralizedLambda(ProjectionDeserializer projectionDeserializer, Lookup lookup) {
    Class<?> lambdaClass = projectionDeserializer.getClass();
    MethodHandle writeReplace = (PRIVATE_LOOKUP_IN == null)?
        findWriteReplaceJava8(lambdaClass, lookup):
        findWriteReplaceJava9(lambdaClass, lookup);
    
    try {
      return (SerializedLambda)writeReplace.invoke(projectionDeserializer);
    } catch (Throwable e) {
      throw Thrower.rethrow(e);
    }
  }
  
  private static MethodHandle findWriteReplaceJava8(Class<?> lambdaClass, Lookup lookup) {
    Method writeReplace;
    try {
      writeReplace = lambdaClass.getDeclaredMethod("writeReplace");
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("the writeReplace method is not found, perhaps it's not a serializable lambad ?", e);
    }
    writeReplace.setAccessible(true);
    
    try {
      return lookup.unreflect(writeReplace);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("the lookup can not access to the method writeReplace", e);
    }
  }
  
  private static MethodHandle findWriteReplaceJava9(Class<?> lambdaClass, Lookup lookup) {
    Lookup teleport;
    try {
      teleport = (Lookup)PRIVATE_LOOKUP_IN.invoke(lambdaClass, lookup);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("the lookup can not access to the lambda proxy class", e);
    } catch(Throwable e) {
      throw Thrower.rethrow(e);
    }
    
    try {
      return teleport.findVirtual(lambdaClass, "writeReplace", MethodType.methodType(Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalArgumentException("the writeReplace method is not accessible, perhaps it's not a serializable lambad ?", e);
    }
  }
  
  
  static String[] extractFieldNames(Lookup lookup, ProjectionFunction<?, ?>[] projections) {
    SerializedLambda[] serializedLambdas = new SerializedLambda[projections.length];
    for(int i = 0; i < serializedLambdas.length; i++) {
      try {
        serializedLambdas[i] = projections[i].asSerializedLambda(lookup);
      } catch(IllegalArgumentException e) {
        throw new IllegalArgumentException("can not extract information from lambda at index " + i, e);
      }
    }
    
    return extractFieldNames(lookup.lookupClass(), serializedLambdas);
  }
  
  private static String methodDesc(SerializedLambda serializedLambda) {
    return serializedLambda.getImplMethodName() + serializedLambda.getImplMethodSignature();
  }
  
  private static String[] extractFieldNames(Class<?> lookupClass, SerializedLambda[] serializedLambdas) {
    Map<String, Map<String, Integer>> implMap =
        range(0, serializedLambdas.length).boxed().collect(groupingBy(
            index -> serializedLambdas[index].getImplClass(), 
            toMap(index -> methodDesc(serializedLambdas[index]), index -> index)));
    String[] fieldNames = new String[serializedLambdas.length];
    
    // find all lambda implementations and scan them
    implMap.forEach((implClassName, methodDescSlotMap) -> {
      byte[] data ;
      try(InputStream input = lookupClass.getResourceAsStream("/" + implClassName.replace('.', '/') + ".class")) {
        if (input == null) {
          throw new IllegalArgumentException("can not access to bytecode of " + implClassName + " using lookup " + lookupClass.getName());
        }
        
        data = readAllBytes(input);
      } catch(IOException e) {
        throw new AssertionError(e);
      }
      
      scanClassFile(data, methodDescSlotMap.keySet(), (methodDesc, fieldName) -> fieldNames[methodDescSlotMap.get(methodDesc)] = fieldName);
    });
    
    // verify that all field names have been extracted
    for(int i = 0; i < fieldNames.length; i++) {
      if (fieldNames[i] == null) {
        throw new IllegalArgumentException("lambda " + i + " doesn't acces to a field name");
      }
    }
    
    return fieldNames;
  }

  private static byte[] readAllBytes(InputStream input) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    int total = 0;
    while((read = input.read(buffer, total, buffer.length - total)) != -1) {
      total += read;
      if (read == 0) {
        buffer = Arrays.copyOf(buffer, buffer.length + 8192);
      }
    }
    return Arrays.copyOf(buffer, total);
  }
  
  
  private static final int UTF8_TAG = 1;
  
  // size of the constant pool item depending on its tag
  private static int[] CONSTANT_SIZE = new int[] {
    0,
    0,  // UTF8
    0,  
    5,  // INTEGER
    5,  // FLOAT
    9,  // LONG
    9,  // DOUBLE
    3,  // CLASS
    3,  // STRING
    5,  // FIEDREF
    5,  // METHODREF
    5,  // ITF_METHODREF
    5,  // NAME_AND_TYPE
    0,  
    0,
    4,  // METHODHANDLE
    3,  // METHODTYPE
    5,  // CONDY
    5,  // INDY
    3,  // MODULE
    3,  // PACKAGE
  };
  
  private static final int ALOAD_0 = 42;
  private static final int GETFIELD = 180;
  private static final int INVOKESTATIC = 184;
  private static final int ARETURN = 176;
  
  private static int readU2(byte[] data, int offset) {
    return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
  }
  
  private static int readU4(byte[] data, int offset) {
    return ((data[offset] & 0xFF) << 24)
        | ((data[offset + 1] & 0xFF) << 16)
        | ((data[offset + 2] & 0xFF) << 8)
        | (data[offset + 3] & 0xFF);
  }

  private static String string(byte[] data, int[] offsets, String[] cache, int offset) {
    int index = readU2(data, offset);
    
    String s = cache[index];
    if (s != null) {
      return s;
    }
    // read String item
    int itemOffset = offsets[index];
    return cache[index] = readUTF8(data, itemOffset + 2, readU2(data, itemOffset));
  }

  private static String readUTF8(byte[] data, int utfOffset, int utfLength) {
    int offset = utfOffset;
    int end = offset + utfLength;
    int stringIndex = 0;

    char[] buffer = new char[utfLength]; // may be bigger than necessary
    while (offset < end) {
      int b = data[offset++];
      if ((b & 0x80) == 0) {
        buffer[stringIndex++] = (char) (b & 0x7F);
      } else if ((b & 0xE0) == 0xC0) {
        buffer[stringIndex++] = (char) (((b & 0x1F) << 6) | (data[offset++] & 0x3F));
      } else {
        buffer[stringIndex++] = (char) (((b & 0xF) << 12) | ((data[offset++] & 0x3F) << 6) | (data[offset++] & 0x3F));
      }
    }
    return new String(buffer, 0, stringIndex);
  }

  
  private static void scanClassFile(byte[] data, Set<String> methodDescSet, BiConsumer<String, String> consumer) {
    int constantsCount = readU2(data, 8);
    int[] offsets = new int[constantsCount];
    
    // scan the constant pool items, find their offsets
    int offset = 10;
    for (int i = 1; i < constantsCount; i++) {
      offsets[i] = offset + 1;
      
      int constant = data[offset];
      offset += CONSTANT_SIZE[constant];
      if (constant == UTF8_TAG) {
        offset += 3 + readU2(data, offset + 1);
      } 
    }
    
    // skip header
    int interfacesCount = readU2(data, offset + 6);
    offset += 8 + 2 * interfacesCount;
    
    // skip fields
    int fieldsCount = readU2(data, offset);
    offset += 2;
    for (int i = 0; i < fieldsCount; i++) {
      int attributesCount = readU2(data, offset + 6);
      offset += 8;
      // Skip field attributes
      for(int j = 0; j < attributesCount; j++) {
        offset += 6 + readU4(data, offset + 2);
      }
    }

    String[] cache = new String[constantsCount];
    
    // scan methods
    int methodCount = readU2(data, offset);
    offset += 2;
    for (int i = 0; i < methodCount; i++) {
      
      String methodDesc = string(data, offsets, cache, offset + 2) + string(data, offsets, cache, offset + 4);
      
      int attributesCount = readU2(data, offset + 6);
      offset += 8;
      
      if (methodDescSet.contains(methodDesc)) {
        // scan to find the "Code" attribute
        for(int j = 0; j < attributesCount; j++) {
          String attributeName = string(data, offsets, cache, offset);
          if (attributeName.equals("Code")) {
            
            int codeOffset = offset + 6;
            int codeLength = readU4(data, codeOffset + 4);
            codeOffset += 8;
            int codeEnd = codeOffset + codeLength;
            
            String fieldName = null;
            
            loop: while(codeOffset < codeEnd) {
              int opcode = data[codeOffset] & 0xFF;
              switch(opcode) {
              case ALOAD_0:
                codeOffset++;
                break;
              case GETFIELD: {
                int fieldRefOffset = offsets[readU2(data, codeOffset + 1)];
                int nameAndTypeOffset = offsets[readU2(data, fieldRefOffset + 2)];
                fieldName = string(data, offsets, cache, nameAndTypeOffset);
                codeOffset += 3;
                break;
              }
              case INVOKESTATIC:
                codeOffset += 3;
                break;
              case ARETURN:
                codeOffset++;
                break;
              default:
                fieldName = null;  // mark unrecognized
                break loop;
              }
            }
            
            if (fieldName != null) {
              // pattern fully recognized !!
              consumer.accept(methodDesc, fieldName);
            }
          }
          
          offset += 6 + readU4(data, offset + 2);
        }
        
      } else {
        // Skip method attributes
        for(int j = 0; j < attributesCount; j++) {
          offset += 6 + readU4(data, offset + 2);
        }
      }
    }
  }
}
