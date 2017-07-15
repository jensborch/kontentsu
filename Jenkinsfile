#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            when {
                isUnix()
            } 
            steps {
                sh 'gradle build'
            }
        }
        stage('Static code analysis') {
            when {
                isUnix()
            } 
            steps {
                sh 'gradle pmd'
                sh 'gradle checkstyle'
                sh 'gradle findbugs'
            }
        }   
        stage('Integration tests') {
            when {
                isUnix()
            }
            steps {
                sh 'gradle integrationTest'
            }
        }
    }
}