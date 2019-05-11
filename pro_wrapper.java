import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.list;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Files.write;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

class pro_wrapper {
  private static final String GITHUB_API_RELEASES = "https://github.com/forax/pro/releases";
  private static final String GITHUB_DOWNLOAD = "https://github.com/forax/pro/releases/download";
  private static final Pattern PATTERN = Pattern.compile("tag/([^\"]+)\"");
  
  private static String platform() {
    var osName = getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") != -1) {
      return "windows";
    }
    if (osName.indexOf("mac") != -1) {
      return "macos";
    }
    return "linux";
  }
  
  private static String shell() {
    var osName = getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") != -1) {
      return "cmd.exe";
    }
    return Optional.ofNullable(System.getenv("SHELL")).filter(not(String::isEmpty)).orElse("/bin/sh");
  }
  
  private static Optional<String> specialBuild() throws IOException {
    var specialBuild = System.getenv("PRO_SPECIAL_BUILD");
    var path = Path.of("pro_wrapper_settings.txt");
    var proSettings = new Properties();
    if (exists(path)) {
      try(var reader = newBufferedReader(path)) {
        proSettings.load(reader);
      }
    }
    return Optional.ofNullable(specialBuild).filter(not(String::isEmpty)).or(
        () -> Optional.ofNullable(proSettings.getProperty("PRO_SPECIAL_BUILD")));
  }
  
  private static String userHome() {
    return System.getProperty("user.home");
  }
  
  private static Optional<String> lastestReleaseVersionFromGitHub() throws IOException {
    System.out.println("try to find latest release on Github ...");
    var url = new URL(GITHUB_API_RELEASES);
    try {
      InetAddress.getByName(url.getHost());  // is internet accessible ?
    } catch(@SuppressWarnings("unused") UnknownHostException e) {
      return Optional.empty();
    }
    try(var input = url.openStream();
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        var buffered = new BufferedReader(reader, 8192)) {
      return buffered.lines()
        .flatMap(line -> Optional.of(PATTERN.matcher(line)).filter(Matcher::find).map(matcher -> matcher.group(1)).stream())
        .findFirst();
    }
  }
  
  private static FileTime lastModified(Path path) {
    try {
      return getLastModifiedTime(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  private static Optional<String> latestReleaseVersionFromCache(Path cache, String filename) {
    System.out.println("try to find latest release in the cache ...");
    try {
      return walk(cache)
          .filter(p -> p.getFileName().toString().equals(filename))
          .sorted(reverseOrder(comparing(p -> lastModified(p))))
          .findFirst().map(p -> p.getParent().getFileName().toString());
    } catch (@SuppressWarnings("unused") IOException | UncheckedIOException e) {
      return Optional.empty();
    }
  }
  
  private static void download(String release, String filename, Path path) throws IOException {
    var url = new URL(GITHUB_DOWNLOAD + "/"+ release + "/" + filename);
    System.out.println("download " + url + " to " + path);
    
    createDirectories(path.getParent());
    try(var input = url.openStream();
        var output = newOutputStream(path)) {
      int read;
      var sum = 0;
      var buffer = new byte[8192];
      while((read = input.read(buffer)) != -1) {
        output.write(buffer, 0, read);
        
        sum += read;
        if (sum >= 1_000_000) {
          System.out.print(".");
          sum = 0;
        }
      }
    }
    System.out.println("");
  }
  
  private static void setExecutionPermissions(Path path) throws IOException {
    var permissions = getPosixFilePermissions(path);
    Collections.addAll(permissions, OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE);
    setPosixFilePermissions(path, permissions);
  }
  
  private static void unpack(Path localPath, Path folder) throws IOException {
    System.out.println("unpack pro to " + folder);
    createDirectories(folder);
    try(var zip = new ZipFile(localPath.toFile())) {
      for(var entry: Collections.list(zip.entries())) {
        var path = folder.resolve(entry.getName());
        if (entry.isDirectory()) {
          createDirectories(path);
          continue;
        }
        try(var input = zip.getInputStream(entry);
            var output = newOutputStream(path)) {
          input.transferTo(output);
        }
      }
    }
    
    // make the commands in pro/bin, and pro/lib executable
    for(var cmd: (Iterable<Path>)list(folder.resolve("pro").resolve("bin"))::iterator) {
      setExecutionPermissions(cmd);
    }
    for(var path: (Iterable<Path>)list(folder.resolve("pro").resolve("lib"))::iterator) {
      var fileName = path.getFileName().toString();
      if (fileName.equals("jexec") || fileName.equals("jspawnhelper")) {
        setExecutionPermissions(path);
      }
    }
  }
  
  private static void deleteAllFiles(Path directory) throws IOException {
    if (!exists(directory)) {
      return;
    }

    walkFileTree(directory, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        delete(path);
        return super.postVisitDirectory(path, e);
      }
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        delete(path);
        return FileVisitResult.CONTINUE;
      }
    });
  }
  
  private static String firstLine(Path path) throws IOException {
    return Optional.of(readAllLines(path)).filter(lines -> !lines.isEmpty()).map(lines -> lines.get(0)).orElse("");
  }

  private static int exec(String command, String[] args) throws IOException {
    var builder = new ProcessBuilder(Stream.of(Stream.of(shell()), Stream.of(command), Arrays.stream(args)).flatMap(x -> x).toArray(String[]::new));
    var process = builder.inheritIO().start();
    try {
      return process.waitFor();
    } catch (InterruptedException e) {
      throw new IOException(e);
    }
  }
  
  public interface PathConsumer {
    void accept(Path path) throws IOException;
  }
  
  private static void retry(Path resource, int times, PathConsumer consumer) throws IOException {
    if (times <= 0) {
      throw new IllegalArgumentException("times <= 0");
    }
    IOException exception = null;
    var count = times;
    do {
      try {
        consumer.accept(resource);
        return;
      } catch(IOException e) {
        if (exception == null) {
          exception = new IOException();
        }
        exception.addSuppressed(e);
        
        // cleanup
        Files.deleteIfExists(resource);
        
        System.out.println("download fails ... retry !");
      }
    } while(--count != 0);
    throw exception;
  }
  
  private static int installAndRun(String[] args) throws IOException {
    var specialBuild = specialBuild().map(build -> '-' + build).orElse("");
    var filename = "pro-" + platform() + specialBuild + ".zip";
    
    var cache = Path.of(userHome(), ".pro", "cache");
    var release = lastestReleaseVersionFromGitHub()
        .or(() -> latestReleaseVersionFromCache(cache, filename))
        .orElseThrow(() -> new IOException("no release found !"));
    
    
    System.out.println("require " + filename + " release " + release + " ...");
    
    var cachePath = cache.resolve(Path.of(release, filename));
    if (!exists(cachePath)) {
      retry(cachePath, 3, _cachedPath -> download(release, filename, _cachedPath));
    }
    
    var releaseTxt = Path.of("pro", "pro-release.txt");
    if (!exists(releaseTxt) || !firstLine(releaseTxt).equals(filename)) {
      deleteAllFiles(releaseTxt.getParent());
      
      unpack(cachePath, Path.of("."));
      write(releaseTxt, List.of(filename));
    }
    
    return exec("pro/bin/pro", args);
  }
  
  public static void main(String[] args) {
    try {
      exit(installAndRun(args));
    } catch(IOException e) {
      System.err.println("i/o error " + e.getMessage() +
          Optional.ofNullable(e.getStackTrace()).filter(stack -> stack.length > 0).map(stack -> " at " + stack[0]).orElse(""));
      exit(1);
    }
  }
}