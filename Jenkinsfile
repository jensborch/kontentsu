#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'gradle build'
            }
        }
        stage('Static code analysis') {
            steps {
                sh 'gradle pmd'
                sh 'gradle checkstyle'
                sh 'gradle findbugs'
            }
        }   
        stage('Integration tests') {
            steps {
                sh 'gradle integrationTest'
            }
        }
    }
}