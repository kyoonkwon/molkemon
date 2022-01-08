package com.example.week2;

import java.io.Serializable;

public class Skill implements Serializable {
    int id;
    String name;
    Double cool;
    Integer skillcoin;
    int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSkillcoin() {
        return skillcoin;
    }

    public void setSkillcoin() {
        this.skillcoin =(this.id%10)*this.level;
    }

    public Skill(int id, String name, Double cool, int level, int power, Integer skillcoin) {
        this.id = id;
        this.name = name;
        this.cool = cool;
        this.level = level;
        this.power = power;
        this.skillcoin = skillcoin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCool() {
        return cool;
    }

    public void setCool(Double cool) {
        this.cool = cool;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    int power;
}
