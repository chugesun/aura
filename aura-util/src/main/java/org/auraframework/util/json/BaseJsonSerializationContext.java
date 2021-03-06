/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.util.json;

/**
 * standard jsony stuff
 */
public abstract class BaseJsonSerializationContext implements JsonSerializationContext {
    private final boolean format;
    private int formatRootItems = 0;
    private final int dataSizeLimit;
    private final int collectionSizeLimit;
    private boolean nullValues;
    private boolean isSerializing = false;

    public BaseJsonSerializationContext(boolean format, int dataSizeLimit, int collectionSizeLimit,
                                        boolean nullValues) {
        this.format = format;
        this.dataSizeLimit = dataSizeLimit;
        this.collectionSizeLimit = collectionSizeLimit;
        this.nullValues = nullValues;
    }

    @Override
    public boolean format() {
        return format;
    }

    @Override
    public boolean formatRootItems() {
        return formatRootItems > 0;
    }

    @Override
    public void pushFormatRootItems() {
        formatRootItems++;
    }

    @Override
    public void popFormatRootItems() {
        if (formatRootItems > 0) {
            formatRootItems--;
        }
    }

    @Override
    public int getVariableDataSizeLimit() {
        return dataSizeLimit;
    }

    /**
     * Don't render collections over this length, -1 to not truncate
     */
    @Override
    public int getCollectionSizeLimit() {
        return collectionSizeLimit;
    }

    @Override
    public boolean isNullValueEnabled() {
        return nullValues;
    }

    @Override
    public boolean setNullValueEnabled(boolean nullValueEnabled) {
        boolean old = nullValues;

        nullValues = nullValueEnabled;
        return old;
    }

    @Override
    public void setSerializing(boolean serializing) {
        this.isSerializing = serializing;
    }

    @Override
    public boolean isSerializing() {
        return this.isSerializing;
    }
}
