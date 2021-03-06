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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.def.AttributeDef;
import org.auraframework.def.Definition;
import org.auraframework.def.DefinitionReference;
import org.auraframework.impl.root.AttributeDefRefImpl;
import org.auraframework.impl.root.component.DefRefDelegate;
import org.auraframework.impl.util.TextTokenizer;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.TextSource;
import org.auraframework.throwable.quickfix.QuickFixException;

import com.google.common.collect.ImmutableSet;

/**
 * {@code <aura:set>} tags
 */
public class AttributeDefRefHandler<P extends Definition> extends ParentedTagHandler<AttributeDefRefImpl, P> {

    public static final String TAG = "aura:set";

    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_ATTRIBUTE = "attribute";

    private final static Set<String> ALLOWED_ATTRIBUTES = ImmutableSet.of(ATTRIBUTE_VALUE, ATTRIBUTE_ATTRIBUTE);

    private final AttributeDefRefImpl.Builder builder = new AttributeDefRefImpl.Builder();
    private final List<DefinitionReference> children = new ArrayList<>();
    private String stringValue;

    public AttributeDefRefHandler(XMLStreamReader xmlReader, TextSource<?> source, DefinitionService definitionService,
                                  boolean isInInternalNamespace, ConfigAdapter configAdapter,
                                  DefinitionParserAdapter definitionParserAdapter, ContainerTagHandler<P> parentHandler) {
        super(xmlReader, source, definitionService, isInInternalNamespace, configAdapter, definitionParserAdapter, parentHandler);
        builder.setLocation(getLocation());
        builder.setAccess(getAccess(isInInternalNamespace));
    }

    @Override
    public Set<String> getAllowedAttributes() {
        return ALLOWED_ATTRIBUTES;
    }

    @Override
    protected void readAttributes() {
        builder.setDescriptor(definitionService.getDefDescriptor(getAttributeValue(ATTRIBUTE_ATTRIBUTE), AttributeDef.class));
        stringValue = getAttributeValue(ATTRIBUTE_VALUE);
    }

    @Override
    protected void finishDefinition() throws QuickFixException {
        if (StringUtils.isBlank(stringValue)) {
            if(!children.isEmpty()) {
                builder.setValue(children);
            } else {
                builder.setValue("");
            }
        } else {
            TextTokenizer tt = TextTokenizer.tokenize(stringValue, getLocation());
            builder.setValue(tt.asValue(getParentHandler()));
        }
    }

    @Override
    protected AttributeDefRefImpl createDefinition() throws QuickFixException {
        return builder.build();
    }

    @Override
    public String getHandledTag() {
        return AttributeDefRefHandler.TAG;
    }

    @Override
    protected void handleChildTag() throws XMLStreamException, QuickFixException {
        DefinitionReference dr = new DefRefDelegate(getDefRefHandler(getParentHandler()).getElement());
        children.add(dr);
    }

    @Override
    protected void handleChildText() throws XMLStreamException, QuickFixException {
        children.addAll(tokenizeChildText());
    }
}
