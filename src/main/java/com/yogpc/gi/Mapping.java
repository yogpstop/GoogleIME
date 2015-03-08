package com.yogpc.gi;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.commons.Remapper;

public class Mapping extends Remapper {
  public static final Mapping I = new Mapping();
  private static final Map<String, String> cls = new HashMap<String, String>();
  private static final Map<String, Map<String, String>> mtd =
      new HashMap<String, Map<String, String>>();

  static void addC(final String c_raw, final String c_obf) {
    cls.put(c_raw, c_obf);
  }

  static String addM(final String c_raw, final String m_raw, final String m_obf) {
    Map<String, String> min = mtd.get(c_raw);
    if (min == null)
      mtd.put(c_raw, min = new HashMap<String, String>());
    min.put(m_raw, m_obf);
    return m_obf;
  }

  @Override
  public String mapFieldName(final String owner, final String name, final String desc) {
    return mapMethodName(owner, name, desc);
  }

  @Override
  public String mapMethodName(final String owner, final String name, final String desc) {
    if (owner.startsWith("com/yogpc/gi/dummy/")) {
      final Map<String, String> min = mtd.get(owner.substring(19));
      if (min != null) {
        final String tmp = min.get(name);
        if (tmp != null)
          return tmp;
      }
    }
    return name;
  }


  @Override
  public String map(final String typeName) {
    if (typeName.startsWith("com/yogpc/gi/dummy/")) {
      final String tmp = cls.get(typeName.substring(19));
      if (tmp != null)
        return tmp;
    }
    return typeName;
  }
}
