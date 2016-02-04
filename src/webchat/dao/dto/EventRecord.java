package webchat.dao.dto;

@Table(name = "Events")
public class EventRecord {

	@Column(name = "Typex")
	private String type;
	
	@Column(name = "Id", isPrimaryKey = true)
	private Integer id;
	
	@Column(name = "SourceId", isForeignKeyOf="webchat.dao.dto.UserRecord")
	private Integer sourceId;
	
	@Column(name = "TargetId", isForeignKeyOf="webchat.dao.dto.UserRecord")
	private Integer targetId;
	
        
        @Reference(foreignKeyField = "sourceId", foreignField = "username")
	@Column(name = "SourceName")
	private String sourceName;
	
        @Reference(foreignKeyField = "targetId", foreignField = "username")
	@Column(name = "TargetName")
	private String targetName;

	@Column(name = "RoomName")
	private String roomName;
	
	@Column(name = "Message")
	private String message;
	
	@Column(name = "Timestampx")
	private Long timestamp;


        
	public EventRecord(String type, int srcId) {
		super();
		this.type = type;
		this.sourceId = srcId;
	}

	public EventRecord() {
		
	}
	
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public Integer getSourceId() {
		return sourceId;
	}


	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}


	public Integer getTargetId() {
		return targetId;
	}


	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}


	public String getSourceName() {
		return sourceName;
	}


	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}


	public String getTargetName() {
		return targetName;
	}


	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}


	public String getRoomName() {
		return roomName;
	}


	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}
	

	

}
