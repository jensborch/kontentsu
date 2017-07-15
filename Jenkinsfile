#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        node {
        stage('Build') {
                if(isUnix()) {
                    sh 'gradle build'
                } else {
                    bat 'gradle build'
                }
        }
        stage('Static code analysis') {
                if(isUnix()) {
                    sh 'gradle pmd'
                    sh 'gradle checkstyle'
                    sh 'gradle findbugs'
                } else {
                    bat 'gradle pmd'
                    bat 'gradle checkstyle'
                    bat 'gradle findbugs'                    
                }
        }   
        stage('Integration tests') {
                if(isUnix()) {
                    sh 'gradle integrationTest'
                } else {
                    bat 'gradle integrationTest'
                }
        }
    }
    }
}