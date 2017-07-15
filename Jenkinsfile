#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                node {
                if(isUnix()) {
                    sh 'gradle build'
                } else {
                    bat 'gradle build'
                }
                }
            }
        }
        stage('Static code analysis') {
            steps {
                node {
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
            }
        }   
        stage('Integration tests') {
            steps {
                node {
                if(isUnix()) {
                    sh 'gradle integrationTest'
                } else {
                    bat 'gradle integrationTest'
                }
                }
            }
        }
    }
}