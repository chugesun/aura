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
package org.auraframework.impl.root.parser.handler;

import static org.auraframework.impl.root.parser.handler.RootTagHandler.ATTRIBUTE_DESCRIPTION;

import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.def.TokenDef;
import org.auraframework.def.TokensDef;
import org.auraframework.impl.css.token.TokenDefImpl;
import org.auraframework.impl.util.TextTokenizer;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.TextSource;
import org.auraframework.throwable.quickfix.InvalidAccessValueException;
import org.auraframework.throwable.quickfix.QuickFixException;

import com.google.common.collect.ImmutableSet;

public final class TokenDefHandler extends ParentedTagHandler<TokenDef, TokensDef> {
    protected static final String TAG = "aura:token";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_PROPERTY = "property";

    private static final Set<String> ALLOWED_ATTRIBUTES = new ImmutableSet.Builder<String>()
            .add(ATTRIBUTE_ACCESS, ATTRIBUTE_NAME, ATTRIBUTE_VALUE, ATTRIBUTE_PROPERTY)
            .addAll(RootTagHandler.ALLOWED_ATTRIBUTES)
            .build();

    private static final Set<String> INTERNAL_ALLOWED_ATTRIBUTES = ImmutableSet.copyOf(ALLOWED_ATTRIBUTES);

    private final TokenDefImpl.Builder builder = new TokenDefImpl.Builder();
    private String value;

    public TokenDefHandler(XMLStreamReader xmlReader, TextSource<?> source, DefinitionService definitionService,
                           boolean isInInternalNamespace, ConfigAdapter configAdapter,
                           DefinitionParserAdapter definitionParserAdapter, TokensDefHandler parentHandler) {
        super(xmlReader, source, definitionService, isInInternalNamespace, configAdapter, definitionParserAdapter, parentHandler);
        this.builder.setLocation(getLocation());
        this.builder.setConfigAdapter(configAdapter);
    }

    @Override
    public String getHandledTag() {
        return TAG;
    }

    @Override
    public Set<String> getAllowedAttributes() {
        return isInInternalNamespace() ? INTERNAL_ALLOWED_ATTRIBUTES : ALLOWED_ATTRIBUTES;
    }

    @Override
    protected void readAttributes() {
        String name = getAttributeValue(ATTRIBUTE_NAME);
        if (StringUtils.isBlank(name)) {
            // normally this check would be handled by TokenDefImpl#validateDefinition, but waiting till then would
            // result in the get def descriptor below throwing a nondescript error message
            error("Missing required attribute 'name' on %s", TAG);
        }
        builder.setDescriptor(definitionService.getDefDescriptor(name, TokenDef.class));

        value = getAttributeValue(ATTRIBUTE_VALUE);// value (to be set on builder later, cuz it might throw a QFE)

        String allowedProperties = getAttributeValue(ATTRIBUTE_PROPERTY); // comma-separated list of property names
        if (!StringUtils.isBlank(allowedProperties)) {
            builder.setAllowedProperties(allowedProperties);
        }

        try {
            builder.setAccess(readAccessAttribute());
        } catch (InvalidAccessValueException e) {
            builder.setParseError(e);
        }

        builder.setDescription(getAttributeValue(ATTRIBUTE_DESCRIPTION));
        builder.setParentDescriptor(getParentDefDescriptor());
    }

    @Override
    protected void finishDefinition() throws QuickFixException {
        TextTokenizer tt = TextTokenizer.tokenize(value, getLocation());
        builder.setValue(tt.asValue(getParentHandler()));
    }

    @Override
    protected TokenDef createDefinition() throws QuickFixException {
        return builder.build();
    }

    @Override
    protected void handleChildTag() throws XMLStreamException, QuickFixException {
        error("No children allowed for %s tag", TAG);
    }

    @Override
    protected void handleChildText() throws XMLStreamException, QuickFixException {
        if (!StringUtils.isBlank(xmlReader.getText())) {
            error("No literal text allowed in %s tag", TAG);
        }
    }
}
