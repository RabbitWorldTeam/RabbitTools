package com.tr4nce.rabbittools.basics.option;

import net.minecraft.core.component.DataComponentType;
import com.tr4nce.rabbittools.RabbitTools;

public interface IOption<T>
{
    DataComponentType<?> getComponentType();

    String getKey();

    String getValueString();

    void setValueString(String val);

    default String getKeyTranslation() {
        return RabbitTools.MODID + ".option." + getKey();
    }

    default String getValueTranslation() {
        return RabbitTools.MODID + ".option." + getKey() + "." + getValueString();
    }

    default String getDescTranslation() {
        return RabbitTools.MODID + ".option." + getKey() + "." + getValueString() + ".desc";
    }

    boolean isEnabled();

    void set(T val);

    T get();

    T next(boolean dir);

    default T next() {
        return next(true);
    }
}
