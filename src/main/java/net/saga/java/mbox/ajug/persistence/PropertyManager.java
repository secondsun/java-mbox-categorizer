/*
 * Copyright (C) 2016 Your Organisation.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.saga.java.mbox.ajug.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author summers
 */
public class PropertyManager {

    private static final Path PROPERTY_PATH = Paths.get(System.getProperty("user.home"), ".mboxClassifier");
    private static Properties properties = null;

    private static void open() {

        properties = new Properties();
        try {
            if (!Files.exists(PROPERTY_PATH)) {
                Files.createFile(PROPERTY_PATH);
            } else {
                try (BufferedReader propertiesReader = Files.newBufferedReader(PROPERTY_PATH)) {
                    properties.load(propertiesReader);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private static void close() {
        if (properties == null) {
            throw new IllegalStateException("The properties file is not opened.");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(PROPERTY_PATH)) {
            properties.store(writer, "-- nocomment --");
        } catch (IOException ex) {
            Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        properties = null;
    }

    public synchronized static Optional<String> read( String property) {
        if (property == null) {
            throw new IllegalArgumentException("property may not be null");
        }
        try {
            open();
            return Optional.ofNullable(properties.getProperty(property));
        } finally {
            close();
        }

    }

    public synchronized static void write( String property,  String value) {

        if (property == null) {
            throw new IllegalArgumentException("property may not be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        open();
        properties.put(property, value);
        close();
    }

    public synchronized static void unset( String property) {
        if (property == null) {
            throw new IllegalArgumentException("property may not be null");
        }
        open();
        properties.remove(property);
        close();
    }

}
