package asap.bml.ext.ssml.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;

public class SSMLBehaviourBuilder
{
    private final BehaviourBuilder builder;
    private String ssmlContent = "";
    private String bmlContent = "";

    public SSMLBehaviourBuilder(String bmlId, String id)
    {
        builder = new BehaviourBuilder(SpeechBehaviour.xmlTag(), bmlId, id);
    }

    public SSMLBehaviourBuilder ssmlContent(String content)
    {
        ssmlContent = content;
        String str = content.replaceAll("<mark\\s+name", "{sync id");
        str = str.replaceAll("</mark>", "{/sync}");
        bmlContent = str.replaceAll("<.*?>", "");
        bmlContent = bmlContent.replaceAll("\\{sync", "<sync");
        bmlContent = bmlContent.replaceAll("\\{/sync\\}", "</sync>");
        return this;
    }

    public Behaviour build()
    {
        builder.content("<text>" + bmlContent + "</text>" + "<description type=\"application/ssml+xml\" priority=\"1\">"
                + "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\">" + ssmlContent + "</speak>" + "</description>");
        return builder.build();
    }
}
