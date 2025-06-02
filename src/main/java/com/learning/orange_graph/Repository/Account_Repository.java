package com.learning.orange_graph.Repository;

import com.learning.orange_graph.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Account_Repository extends JpaRepository<Account, Long> { }
