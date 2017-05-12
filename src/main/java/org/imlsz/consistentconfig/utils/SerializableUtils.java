package org.imlsz.consistentconfig.utils;

import java.io.*;

/**
 * Created by lsz on 2017/5/12.
 */
public class SerializableUtils {
    public static byte[] encode(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }
    public static Object decode(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object object = ois.readObject();
        return object;
    }
}
