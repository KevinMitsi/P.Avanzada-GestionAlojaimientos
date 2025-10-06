package com.avanzada.alojamientos.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface UserDetailsService {

    UserDetails loadUserByUsername(String id) throws UsernameNotFoundException;

}
