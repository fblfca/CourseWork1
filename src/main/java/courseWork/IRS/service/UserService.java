package courseWork.IRS.service;

import courseWork.IRS.model.Role;
import courseWork.IRS.model.UserInfo;
import courseWork.IRS.repository.RoleRepository;
import courseWork.IRS.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserInfoRepository userInfoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public void registerUser(String email, String password, String name, String surname, String phone) {
        if (roleRepository.findByLogin(email).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        Role role = new Role();
        role.setLogin(email);
        role.setPasswordHash(passwordEncoder.encode(password));
        role.setRole("ПОЛЬЗОВАТЕЛЬ");
        role = roleRepository.save(role);

        UserInfo userInfo = new UserInfo();
        userInfo.setId(role.getId());
        userInfo.setName(name);
        userInfo.setSurname(surname);
        userInfo.setPhone(phone);
        userInfoRepository.save(userInfo);
    }

    public UserInfo findByLogin(String login) {
        Optional<Role> roleOpt = roleRepository.findByLogin(login);
        if (roleOpt.isEmpty()) return null;

        Role role = roleOpt.get();
        UserInfo userInfo = userInfoRepository.findById(role.getId()).orElse(new UserInfo());
        userInfo.setId(role.getId());
        userInfo.setRole(role);
        return userInfo;
    }

    public boolean checkPassword(String login, String rawPassword) {
        Optional<Role> roleOpt = roleRepository.findByLogin(login);
        if (roleOpt.isEmpty()) return false;
        return passwordEncoder.matches(rawPassword, roleOpt.get().getPasswordHash());
    }

    @Transactional
    public void updatePersonalInfo(Integer userId, String name, String surname, String phone) {
        UserInfo info = userInfoRepository.findById(userId).orElseThrow();
        info.setName(name);
        info.setSurname(surname);
        info.setPhone(phone);
        userInfoRepository.save(info);
    }

    @Transactional
    public void updateEmail(Integer userId, String newEmail) {
        if (roleRepository.findByLogin(newEmail).isPresent()) {
            throw new RuntimeException("Email_zanyat");
        }
        Role role = roleRepository.findById(userId).orElseThrow();
        role.setLogin(newEmail);
        roleRepository.save(role);
    }

    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        Role role = roleRepository.findById(userId).orElseThrow();
        role.setPasswordHash(passwordEncoder.encode(newPassword));
        roleRepository.save(role);
    }
}