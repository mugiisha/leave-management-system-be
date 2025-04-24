package org.amir.leavemanagementsystem.repository;

import org.amir.leavemanagementsystem.model.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    List<UserInvitation> findAllByOrderByCreatedAtDesc();
    Optional<UserInvitation> findByToken(String token);
    Optional<UserInvitation> findByEmailAndUsedFalse(String email);
    boolean existsByEmailAndUsedFalse(String email);
} 