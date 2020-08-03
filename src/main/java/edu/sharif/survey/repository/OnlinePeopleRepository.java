package edu.sharif.survey.repository;

import edu.sharif.survey.domain.Subscription;
import edu.sharif.survey.usermanagement.model.entity.User;
import edu.sharif.survey.util.exceptions.Message;
import edu.sharif.survey.util.exceptions.MessageException;

import java.util.*;


public class OnlinePeopleRepository {

    private static OnlinePeopleRepository instance = new OnlinePeopleRepository();
    private Map<Integer, Member> usersInAuction;
    private Map<String, Subscription> subscriptionIds;
    private Map<String,Integer> deviceIds;

    class Member {
        List<User> users = new ArrayList<>();
        User owner;

        void addUser(User user) {
            users.add(user);
        }

        void addOwner(User user) {
            owner = user;
        }

        void removeUser(User user) {
            users.remove(user);
            if (user != null && user.equals(owner))
                owner = null;
        }
    }

    private OnlinePeopleRepository() {
        usersInAuction = new HashMap<>();
        subscriptionIds = new HashMap<>();
        deviceIds = new HashMap<>();
    }

    public static OnlinePeopleRepository getInstance() {
        return instance;
    }

    public Map<Integer, Member> getUsersInAuction() {
        return usersInAuction;
    }

    public List<User> getMembers(int auctionId) {
        if (usersInAuction.get(auctionId) == null)
            return new ArrayList<>();
        return usersInAuction.get(auctionId).users;
    }


    public boolean isInAuction(int auctionId, User user) {
        if (usersInAuction.keySet().contains(auctionId)) {
            if (usersInAuction.get(auctionId).users.contains(user))
                return true;
        }
        return false;
    }

    public void removeAuction(int auctionId) {
        usersInAuction.remove(auctionId);
    }

    public void ExitUser(User user) {
        Set<Integer> auctions = usersInAuction.keySet();
        auctions.forEach(a -> usersInAuction.get(a).removeUser(user));
    }

    public void addSubscriptionId(String subscriptionId, Subscription subscription) {
        subscriptionIds.put(subscriptionId, subscription);
    }

    public boolean isInAuction(User user){
        Set<Integer> auctions = usersInAuction.keySet();
        for (int auctionId :
                auctions) {
            if (isInAuction(auctionId, user))
                return true;
        }
        return false;
    }

    public boolean userExist(User user){
        System.err.println(deviceIds.containsValue(user.getId()));
        return deviceIds.containsValue(user.getId());
    }

    public void addDeviceId(String deviceId, User user){
        if(userExist(user)){
            if(getDeviceId(user).equals(deviceId))
                return;
            throw new MessageException(Message.USER_IS_USING_ANOTHER_DEVICE);
        }
        deviceIds.put(deviceId,user.getId());

    }

    public Integer getUserId(String deviceId){
        return deviceIds.get(deviceId);
    }

    public String getDeviceId(User user){
        if(!userExist(user)){
            throw new MessageException(Message.USER_NOT_FOUND);
        }
        Set<String> devIds = deviceIds.keySet();
        for (String deviceId :
                devIds) {
            if (deviceIds.get(deviceId).equals(user.getId()))
                return deviceId;
        }
        return null;
    }

    public Subscription getAuctionId(String subscriptionId) {
        return subscriptionIds.get(subscriptionId);
    }

    public void removeDeviceId(String deviceId){
        deviceIds.remove(deviceId);
    }
}
