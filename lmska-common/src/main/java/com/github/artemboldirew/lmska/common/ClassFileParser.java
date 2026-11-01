package com.github.artemboldirew.lmska.common;


import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ClassFileParser {
    public ClassMetadata parse(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return parse(in);
        }
    }

    public ClassMetadata parse(InputStream source) throws IOException {
        DataInputStream in = new DataInputStream(source);
        if (in.readInt() != 0xCAFEBABE) {
            throw new IOException("Not a .class file");
        }
        in.readUnsignedShort();
        in.readUnsignedShort();

        int poolSize = in.readUnsignedShort();
        String[] utf8 = new String[poolSize];
        int[] classNameIndexes = new int[poolSize];
        for (int i = 1; i < poolSize; i++) {
            int tag = in.readUnsignedByte();

            switch (tag) {
                case 1 -> utf8[i] = in.readUTF();
                case 3, 4 -> in.readInt();
                case 5, 6 -> {
                    in.readLong();
                    i++;
                }
                case 7 -> classNameIndexes[i] = in.readUnsignedShort();
                case 8, 16, 19, 20 -> in.readUnsignedShort();
                case 9, 10, 11, 12, 17, 18 -> in.skipNBytes(4);
                case 15 -> in.skipNBytes(3);
                default -> throw new IOException("Unknown constant pool tag: " + tag);
            }
        }

        in.readUnsignedShort();
        int thisClass = in.readUnsignedShort();
        int superClass = in.readUnsignedShort();
        String name = getClassName(thisClass, classNameIndexes, utf8);
        String parent = superClass == 0 ? null : getClassName(superClass, classNameIndexes, utf8);
        Set<String> interfaces = new LinkedHashSet<>();
        int interfaceCount = in.readUnsignedShort();

        for (int i = 0; i < interfaceCount; i++) {
            interfaces.add(getClassName(in.readUnsignedShort(), classNameIndexes, utf8));
        }

        skipMembers(in, utf8);
        skipMembers(in, utf8);

        Set<String> annotations = new LinkedHashSet<>();
        readAttributes(in, utf8, annotations);

        return new ClassMetadata(name, parent, interfaces, annotations);
    }

    private static String getClassName(int classIndex, int[] classNameIndexes, String[] utf8) throws IOException {
        if (classIndex <= 0 || classIndex >= classNameIndexes.length) {
            throw new IOException("Invalid class index: " + classIndex);
        }
        int nameIndex = classNameIndexes[classIndex];
        if (nameIndex <= 0 || nameIndex >= utf8.length || utf8[nameIndex] == null) {
            throw new IOException("Invalid class name index: " + nameIndex);
        }
        return utf8[nameIndex].replace('/', '.');
    }

    private static void skipMembers(DataInputStream in, String[] utf8) throws IOException {
        int count = in.readUnsignedShort();

        for (int i = 0; i < count; i++) {
            in.skipNBytes(6);
            readAttributes(in, utf8, null);
        }
    }

    private static void readAttributes(DataInputStream in, String[] utf8, Set<String> annotations) throws IOException {
        int count = in.readUnsignedShort();

        for (int i = 0; i < count; i++) {
            String attributeName = utf8[in.readUnsignedShort()];
            long length = Integer.toUnsignedLong(in.readInt());

            if (annotations != null && "RuntimeVisibleAnnotations".equals(attributeName)) {
                if (length > Integer.MAX_VALUE) {
                    throw new IOException("Annotation attribute too large");
                }
                byte[] data = new byte[(int) length];
                in.readFully(data);
                readAnnotations(new DataInputStream(new ByteArrayInputStream(data)), utf8, annotations);
            } else {
                in.skipNBytes(length);
            }
        }
    }

    private static void readAnnotations(DataInputStream in, String[] utf8, Set<String> annotations) throws IOException {
        int count = in.readUnsignedShort();

        for (int i = 0; i < count; i++) {
            String descriptor = utf8[in.readUnsignedShort()];
            if (descriptor == null || !descriptor.startsWith("L") || !descriptor.endsWith(";")) {
                throw new IOException("Invalid annotation descriptor: " + descriptor);
            }
            annotations.add(descriptor.substring(1, descriptor.length() - 1).replace('/', '.'));
            int pairs = in.readUnsignedShort();
            for (int j = 0; j < pairs; j++) {
                in.readUnsignedShort();
                skipElementValue(in);
            }
        }
    }

    private static void skipAnnotation(DataInputStream in) throws IOException {
        in.readUnsignedShort();
        int pairs = in.readUnsignedShort();

        for (int i = 0; i < pairs; i++) {
            in.readUnsignedShort();
            skipElementValue(in);
        }
    }

    private static void skipElementValue(DataInputStream in) throws IOException {
        char tag = (char) in.readUnsignedByte();
        switch (tag) {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's', 'c' -> in.readUnsignedShort();
            case 'e' -> in.skipNBytes(4);
            case '@' -> skipAnnotation(in);
            case '[' -> {
                int count = in.readUnsignedShort();
                for (int i = 0; i < count; i++) {
                    skipElementValue(in);
                }
            }
            default -> throw new IOException("Unknown annotation value tag: " + tag);
        }
    }
}
