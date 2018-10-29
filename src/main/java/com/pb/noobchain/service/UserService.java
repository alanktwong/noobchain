package com.pb.noobchain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pb.noobchain.domain.User;
import com.pb.noobchain.service.dto.UserDTO;

/**
 * Service class for managing users.
 */
public interface UserService {

    Optional<User> activateRegistration(String key);

    Optional<User> completePasswordReset(String newPassword, String key);

    Optional<User> requestPasswordReset(String mail);

    User createUser(String login, String password, String firstName, String lastName, String email,
        String imageUrl, String langKey);

    User createUser(UserDTO userDTO);

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl);

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    Optional<UserDTO> updateUser(UserDTO userDTO);

    void deleteUser(String login);

    void changePassword(String password);

    Page<UserDTO> getAllManagedUsers(Pageable pageable);

    Optional<User> getUserWithAuthoritiesByLogin(String login);

    User getUserWithAuthorities(Long id);

    User getUserWithAuthorities();

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    void removeNotActivatedUsers();

    List<String> getAuthorities();
}
