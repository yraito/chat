<%-- 
    Document   : accoutstub
    Created on : Feb 11, 2016, 8:39:36 PM
    Author     : Edward
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<div class = "account-content">
    <div class="profilediv splitdiv">
        <h2>Profile</h2>
        <p class="status"></p>
        <p>Username: ${userRecord.username}</p>
        <p>Email: ${userRecord.email}</p>
        <p>Created: ${userRecord.created}</p>
        <p>Password: <button>Change</button></p>
    </div>
    <div class="agentsdiv splitdiv">
        <h2>Agents</h2>
        <p class="status"></p>
        <table>
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Created</th>
                    <th>UUID</th>
                    <th>Password</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td><button class="create-btn">Create</button></td>
                </tr>
                <c:forEach var="agent" items="${agents}">
                    <tr data-name="${agent.username}">
                        <td>${agent.username}</td>
                        <td>${agent.created}</td>
                        <td>${agent.uuid}</td>
                        <td><button class="pass-btn">Change</button></td>
                        <td><button class="delete-btb">Delete</button></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

    </div>
    <div class="formdiv" id="createagentdiv">
        <form>
            <label for="createAgentName">Name: </label>
            <input type="text" required="required" id="createAgentName" maxlength=35 />
            <input type="submit" value="Submit" />
        </form>
    </div>
    <div class="formdiv" id="changepassdiv">
        <form>
            <label for="changePass">New Password: </label>
            <input type="text" required="required" id="changePass" maxlength=35 />
            <input type="submit" value="Submit" />
        </form>
    </div>

</div>
        <script>
            
        </script>