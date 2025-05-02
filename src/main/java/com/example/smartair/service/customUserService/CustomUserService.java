package com.example.smartair.service.customUserService;

import com.example.smartair.dto.userDto.CustomResponseDTO;
import com.example.smartair.entity.user.CustomUser;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.customUserRepository.CustomUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserService {
    private final CustomUserRepository customUserRepository;
    @Autowired
    public CustomUserService(CustomUserRepository customUserRepository){
        this.customUserRepository = customUserRepository;
   }

    public CustomResponseDTO getCustom(User user){
        Optional<CustomUser> optionalCustomUser = customUserRepository.findCustomUserByEmail(user.getEmail());

        CustomResponseDTO response = new CustomResponseDTO();

        if(optionalCustomUser.isEmpty()){
            response.setTemperature(null);
            response.setMoisture(null);
        }else{
            CustomUser customUser = optionalCustomUser.get();
            response.setTemperature(customUser.getTemperature());
            response.setMoisture(customUser.getMoisture());
        }
        return response;
    }
    public void setCustomTemp(User user, Double customTemp){
        Optional<CustomUser> optionalCustomUser = customUserRepository.findCustomUserByEmail(user.getEmail());

        CustomUser customUser = new CustomUser();
        customUser.setEmail(user.getEmail());
        customUser.setTemperature(customTemp);

        customUserRepository.save(customUser);

    }
    public void updateCustomTemp(User user, Double customTemp){
        Optional<CustomUser> optionalCustomUser = customUserRepository.findCustomUserByEmail(user.getEmail());

        CustomUser customUser = optionalCustomUser.get();
        customUser.setTemperature(customTemp);

        customUserRepository.save(customUser);
    }

    public void setCustomMoi(User user, Double customMoi){
        Optional<CustomUser> optionalCustomUser = customUserRepository.findCustomUserByEmail(user.getEmail());

        CustomUser customUser = new CustomUser();
        customUser.setEmail(user.getEmail());
        customUser.setMoisture(customMoi);

        customUserRepository.save(customUser);

    }
    public void updateCustomMoi(User user, Double customMoi){
        Optional<CustomUser> optionalCustomUser = customUserRepository.findCustomUserByEmail(user.getEmail());

        CustomUser customUser = optionalCustomUser.get();
        customUser.setMoisture(customMoi);

        customUserRepository.save(customUser);
    }
    
}
