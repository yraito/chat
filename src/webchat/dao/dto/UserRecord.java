package webchat.dao.dto;

@Table(name = "Users")
public class UserRecord implements AccountRecord {

    @Column(name = "OwnerId", isForeignKeyOf = "webchat.dao.dto.UserRecord")
    Integer ownerId;

    @Reference(foreignKeyField = "ownerId", foreignField = "username")
    @Column(name = "OwnerName")
    String ownerName;

    @Column(name = "Uuid")
    String uuid;

    @Column(name = "Email")
    String email;

    @Column(name = "Id", isPrimaryKey=true)
    Integer id;

    @Column(name = "Username")
    String username;

    @Column(name = "Passhash")
    String passhash;

    @Column(name = "Created")
    Long created;
    
    @Column(name = "Destroyed")
    Long destroyed;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    
    public Long getDestroyed() {
        return destroyed;
    }

    public void setDestroyed(Long destroyed) {
        this.destroyed = destroyed;
    }
    
    @Override
    public String toString() {
        return "[" + id + ", " + ownerId + ", " + uuid + ", " + username + ", " 
                + passhash + ", " + email + ", " + created + "]";
    }

}
