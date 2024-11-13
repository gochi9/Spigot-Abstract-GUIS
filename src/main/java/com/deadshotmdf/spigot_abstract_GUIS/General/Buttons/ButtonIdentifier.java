package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ButtonIdentifier {
    String value();
}
