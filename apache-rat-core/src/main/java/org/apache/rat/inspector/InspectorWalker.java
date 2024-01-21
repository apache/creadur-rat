package org.apache.rat.inspector;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class InspectorWalker {

    private Inspector inspector;
    
    public InspectorWalker(Inspector inspector) {
        this.inspector = inspector;
    }
    
    public String toString() {
        return dump(0,2,inspector);
    }
    
    private String dump(int indent, int indentIncr, Inspector inspector) {
        String padding = StringUtils.repeat(' ', indent);
        StringBuilder sb = new StringBuilder();
        sb.append(padding).append( inspector.getCommonName() );
        if (StringUtils.isNotBlank(inspector.getParamValue())) {
            sb.append( ": ").append( inspector.getParamValue());
        }
        sb.append( StringUtils.CR );
        inspector.getChildren().forEach( s -> sb.append( dump(indent+indentIncr, indentIncr,  s)));
        return sb.toString();
    }

}
