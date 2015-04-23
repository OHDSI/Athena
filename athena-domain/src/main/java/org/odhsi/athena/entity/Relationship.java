package org.odhsi.athena.entity;


/**
 * Created by GMalikov on 27.03.2015.
 */
public class Relationship {

    private String id;
    private String name;
    private String hierarchical;
    private String definesAncestry;
    private Relationship reverse;
    private Concept concept;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(String hierarchical) {
        this.hierarchical = hierarchical;
    }

    public String getDefinesAncestry() {
        return definesAncestry;
    }

    public void setDefinesAncestry(String definesAncestry) {
        this.definesAncestry = definesAncestry;
    }

    public Relationship getReverse() {
        return reverse;
    }

    public void setReverse(Relationship reverse) {
        this.reverse = reverse;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
