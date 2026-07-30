package com.odysseusinc.athena.model.security;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "tokens")
public class AthenaToken {
    @Id
    @SequenceGenerator(name = "tokens_seq", sequenceName = "tokens_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokens_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AthenaUser user;

    @Column(name = "value")
    private String value;
}
