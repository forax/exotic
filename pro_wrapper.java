import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.list;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Files.write;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    var shell = System.getenv("SHELL");
    if (shell != null && !shell.isEmpty()) {
      return shell;
    }
    return "/bin/sh";
  }
  
  private static Optional<String> specialBuild() {
    var specialBuild = System.getenv("PRO_SPECIAL_BUILD");
    return Optional.ofNullable(specialBuild);
  }
  
  private static String userHome() {
    return System.getProperty("user.home");
  }
  
  private static Optional<String> lastestReleaseVersion() throws IOException {
    var url = new URL(GITHUB_API_RELEASES);
    try(var input = url.openStream();
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        var buffered = new BufferedReader(reader, 8192)) {
      return buffered.lines()
        .flatMap(line -> Optional.of(PATTERN.matcher(line)).filter(Matcher::find).map(matcher -> matcher.group(1)).stream())
        .findFirst();
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
    
    // make the command in pro/bin executable
    for(var cmd: (Iterable<Path>)list(folder.resolve("pro").resolve("bin"))::iterator) {
      Set<PosixFilePermission> permissions = getPosixFilePermissions(cmd);
      Collections.addAll(permissions, OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE);
      setPosixFilePermissions(cmd, permissions);
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
  
  private static int installAndRun(String[] args) throws IOException {
    var release = lastestReleaseVersion().orElseThrow(() -> new IOException("latest release not found on Github"));
    var specialBuild = specialBuild().map(build -> '-' + build).orElse("");
    var filename = "pro-" + platform() + specialBuild + ".zip";
    
    var cachePath = Paths.get(userHome(), ".pro", "cache", release, filename);
    if (!exists(cachePath)) {
      download(release, filename, cachePath);
    }
    
    var releaseTxt = Paths.get("pro", "pro-release.txt");
    if (!exists(releaseTxt) || !firstLine(releaseTxt).equals(release)) {
      deleteAllFiles(releaseTxt.getParent());
      
      unpack(cachePath, Paths.get("."));
      write(releaseTxt, List.of(release));
    }
    
    return exec("pro/bin/pro", args);
  }
  
  public static void main(String[] args) {
    try {
      exit(installAndRun(args));
    } catch(IOException e) {
      System.err.println("i/o error " + e.getMessage() +
          Optional.ofNullable(e.getStackTrace()).filter(stack -> stack.length > 0).map(stack -> " from " + stack[0]).orElse(""));
      exit(1);
    }
  }
}