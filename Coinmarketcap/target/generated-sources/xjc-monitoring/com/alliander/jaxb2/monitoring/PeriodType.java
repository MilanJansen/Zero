//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.08 at 02:43:12 PM CET 
//


package com.alliander.jaxb2.monitoring;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PeriodType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PeriodType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DAILY"/>
 *     &lt;enumeration value="MONTHLY"/>
 *     &lt;enumeration value="INTERVAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PeriodType")
@XmlEnum
public enum PeriodType {

    DAILY,
    MONTHLY,
    INTERVAL;

    public String value() {
        return name();
    }

    public static PeriodType fromValue(String v) {
        return valueOf(v);
    }

}