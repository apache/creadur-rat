package org.apache.rat.ant;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Maps Options to their data processing templates.
 */
public final class ArgMethodMapping {
    /** The template for FileSet processing */
    private final Template fileSetTemplate;
    /** The template for Text processing */
    private final Template textValueTemplate;
    /** The default template */
    private final Template defaultTemplate;

    ArgMethodMapping(final String templatePath, final VelocityEngine velocityEngine) {
        fileSetTemplate = velocityEngine.getTemplate(templatePath + "/fileSetTemplate.vm");
        textValueTemplate = velocityEngine.getTemplate(templatePath + "/textValueTemplate.vm");
        defaultTemplate = velocityEngine.getTemplate(templatePath + "/defaultTypeTemplate.vm");
    }

    /**
     * Gets the template for the option and updates the context to contain any special options.
     * @param antOption the Option to get the template for.
     * @param context the context to update.
     * @return the template of @{code null} if there is no template.
     */
    Template getTemplate(final AntOption antOption, final VelocityContext context) {
        if (antOption.hasArg()) {
            switch (antOption.getArgType()) {
                case FILE:
                case DIRORARCHIVE:
                    context.put("argType", "FileSet");
                    context.put("argName", "fileSet");
                    return fileSetTemplate;
                case NONE:
                    return null;
                case STANDARDCOLLECTION:
                    context.put("argType", "Std");
                    return textValueTemplate;
                case EXPRESSION:
                    context.put("argType", "Expr");
                    return textValueTemplate;
                case COUNTERPATTERN:
                    context.put("argType", "Cntr");
                    return textValueTemplate;
                case LICENSEID:
                case FAMILYID:
                    context.put("argType", "Lst");
                    return textValueTemplate;
                default:
                    return defaultTemplate;
            }
        }
        return null;
    }
}
