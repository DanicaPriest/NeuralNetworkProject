package NNProject.service;

import NNProject.mapper.NNMapper;
import NNProject.model.*;
import NNProject.model.Pet;
import NNProject.model.Randyroot;
import NNProject.repository.RoleRepository;
import NNProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@Service
public class RabbitNameService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    NNMapper nnMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //maps the data from the petfinder api to an object
    public String getRabbits() {

        String webUrl = "http://api.petfinder.com/pet.getRandom?key=9bce8b750600914be2415a1932012ee0&output=basic&format=json&animal=rabbit";

        Randyroot pet = restTemplate.getForObject(webUrl, Randyroot.class);
        String name = pet.getPetfinder().getPet().getName().get$t();


        return name;


    }

    public ArrayList<String> makeList() {

        ArrayList<String> rabbitNames = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {

            String rabbit = getRabbits();
            String name = rabbit.replaceAll(" [^\\w].*", "").replaceAll(" [ at ].*", "").replaceAll("\\d.*", "");
            rabbitNames.add(name);
            insertRN(name);
            System.out.println(name);
        }
        return rabbitNames;
    }


    //inserts names into the mysql database
    public void insertRN(String name) {
        nnMapper.insertRabbitName(name);

    }

    //finds and removes all number and symbol characters from the end of names and deletes names that are not real
    public ArrayList<String> cleanDB() {
        ArrayList<Rabbit> db = nnMapper.getAllNames();
        ArrayList<String> list = new ArrayList();
        for (Rabbit r : db
                ) {
            String name = r.getName().replaceAll(" [^\\w].*", "").replaceAll(" [ at ].*", "").replaceAll("\\d.*", "");
            r.setName(name);
            nnMapper.updateName(r);
            if (name.contains("foster") || name.contentEquals("A") || name.contains("Foster") || name.contains("FOSTER")) {
                System.out.println(name);
                nnMapper.deleteName(r.getRabbit_id());

            }

        }
/*        //resetting the ids after clean up
        ArrayList<Rabbit> newDB = nnMapper.getAllNames();
        int i = 1;
        for (Rabbit t: newDB
             ) {t.setId(i);
             nnMapper.updateID(t);
             i++;

        }*/

//add names to string array so it can use the load.html template
        ArrayList<Rabbit> newdb = nnMapper.getAllNames();
        for (Rabbit rt : newdb
                ) {
            list.add(rt.getName());

        }
        return list;
    }

//methods or spring security auth
    public void saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        Role userRole = roleRepository.findByRole("ADMIN");
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        userRepository.save(user);
    }

    public User findUserByName(String username) {
        return userRepository.findByName(username);
    }

    //join tables for username and rabbit name
        /*public void joinUR(String rabbitname, String username){
        nnMapper.joinUR(rabbitname, username);
        }*/
}
