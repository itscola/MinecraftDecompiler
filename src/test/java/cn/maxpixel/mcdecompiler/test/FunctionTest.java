/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.test;

import cn.maxpixel.mcdecompiler.util.JarUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

public class FunctionTest {
    private static final Logger LOGGER = LogManager.getLogger();
    public void test() throws Throwable {
    }
    public static void zipFsUnzip(Path jar, Path dest) {
        try(FileSystem jarFs = JarUtil.createZipFs(jar);
            Stream<Path> s = Files.walk(jarFs.getPath("/")).parallel()) {
            Path jarRoot = jarFs.getPath("/");
            s.forEach(path -> {
                try {
                    // Use dest.resolve(String) because "path" and "dest" are not in the same FileSystem,
                    // use dest.resolve(Path) will cause java.nio.file.ProviderMismatchException
                    Path p = dest.resolve(jarRoot.relativize(path).toString());
                    if(Files.isDirectory(path)) Files.createDirectories(p);
                    else {
                        Files.createDirectories(p.getParent());
                        try(ReadableByteChannel from = JarUtil.getJarFileSystemProvider().newByteChannel(path, Collections.singleton(READ));
                            FileChannel to = FileChannel.open(p, WRITE, CREATE, TRUNCATE_EXISTING)) {
                            to.transferFrom(from, 0, Long.MAX_VALUE);
                        }
                    }
                } catch (IOException e) {
                    throw Utils.wrapInRuntime(e);
                }
            });
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
}