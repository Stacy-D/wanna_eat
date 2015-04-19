package timeToEat.domain;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class RestLink {
@Id
private Long linkID;
private String link;
private RestLink(){}
public RestLink(Long linkId,String link){
	this.link = link;
	this.linkID = linkId;
}
public String getLink() {
	return link;
}
public Long getLinkID(){
	return this.linkID;
}
}
