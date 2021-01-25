package me.jinhee.jwttutorial.repository;

import me.jinhee.jwttutorial.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    @EntityGraph(attributePaths = "authorities") //@EntityGraph는 쿼리가 수행될 때 Lazy조회(지연로딩)가 아니고 Eager조회(즉시로딩)로 authorities정보를 같이 가져옴
    Optional<User> findOnewWithAuthoritiesByUsername(String userName);
}
