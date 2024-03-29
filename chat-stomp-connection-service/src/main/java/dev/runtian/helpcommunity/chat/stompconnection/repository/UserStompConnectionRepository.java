package dev.runtian.helpcommunity.chat.stompconnection.repository;

import dev.runtian.helpcommunity.chat.commons.stompconnection.UserStompConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStompConnectionRepository extends CrudRepository<UserStompConnection, String> {
}
