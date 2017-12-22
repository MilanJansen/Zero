//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.08 at 02:43:12 PM CET 
//


package com.alliander.jaxb2.monitoring;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OsgpResultType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OsgpResultType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OK"/>
 *     &lt;enumeration value="NOT FOUND"/>
 *     &lt;enumeration value="NOT OK"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "OsgpResultType", namespace = "http://www.alliander.com/schemas/osgp/common/2014/10")
@XmlEnum
public enum OsgpResultType {

    OK("OK"),
    @XmlEnumValue("NOT FOUND")
    NOT_FOUND("NOT FOUND"),
    @XmlEnumValue("NOT OK")
    NOT_OK("NOT OK");
    private final String value;

    OsgpResultType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OsgpResultType fromValue(String v) {
        for (OsgpResultType c: OsgpResultType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
