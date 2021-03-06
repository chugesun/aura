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
package org.auraframework.impl.root.parser.handler.design;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.def.ComponentDefRef;
import org.auraframework.def.DefinitionReference;
import org.auraframework.def.design.DesignAttributeDefaultDef;
import org.auraframework.def.design.DesignDef;
import org.auraframework.impl.design.DesignAttributeDefaultDefImpl;
import org.auraframework.impl.root.component.DefRefDelegate;
import org.auraframework.impl.root.parser.handler.ParentedTagHandler;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.TextSource;
import org.auraframework.throwable.quickfix.QuickFixException;

/**
 * Handler for design attribute default
 */
public class DesignAttributeDefaultDefHandler extends ParentedTagHandler<DesignAttributeDefaultDef, DesignDef> {
    private static final String AURA_HTML = "markup://aura:html";
    public static final String TAG = "design:attributedefault";
    DesignAttributeDefaultDefImpl.Builder builder = new DesignAttributeDefaultDefImpl.Builder();


    public DesignAttributeDefaultDefHandler(XMLStreamReader xmlReader, TextSource<?> source,
                                            DefinitionService definitionService, boolean isInInternalNamespace,
                                            ConfigAdapter configAdapter,
                                            DefinitionParserAdapter definitionParserAdapter,
                                            DesignDefHandler parentHandler) {
        super(xmlReader, source, definitionService, isInInternalNamespace, configAdapter, definitionParserAdapter, parentHandler);
        builder.setAccess(getAccess(isInInternalNamespace));
    }

    @Override
    protected void handleChildTag() throws XMLStreamException, QuickFixException {
        //
        // FIXME: once we make DefRefDelegate go away this is much simpler.
        //
        ComponentDefRef componentRef = getDefRefHandler(getParentHandler()).getElement();
        DefinitionReference ref = new DefRefDelegate(componentRef);
        //For now we only accept adding components to the default.
        if (AURA_HTML.equals(componentRef.getDescriptor().getQualifiedName())) {
            error("HTML tags are disallowed in attribute defaults, only components may be set.");
        }
        builder.addComponentRef(ref);
    }

    @Override
    protected void handleChildText() throws XMLStreamException, QuickFixException {
        if (!StringUtils.isBlank(xmlReader.getText())) {
            error("No literal text allowed in attribute default definition");
        }
    }

    @Override
    public String getHandledTag() {
        return TAG;
    }

    @Override
    protected void finishDefinition() {
    }

    @Override
    protected DesignAttributeDefaultDef createDefinition() throws QuickFixException {
        return builder.build();
    }
}
