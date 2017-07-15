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
                sh 'gradle pmd pmdMain'
                sh 'gradle checkstyle checkstyleMain'
                sh 'gradle findbugs findbugsMain'
            }
        }   
        stage('Integration tests') {
            steps {
                sh 'gradle integrationTest'
            }
        }
        stage("Reports") {
            steps {
                junit '**/build/test-results/test/*.xml'
                jacoco changeBuildStatus: true, exclusionPattern: ' **/*Test*.class, **/*IT.class, **/model/**/Q*.class', maximumBranchCoverage: '60', maximumClassCoverage: '70', maximumComplexityCoverage: '60', maximumInstructionCoverage: '60', maximumLineCoverage: '60', maximumMethodCoverage: '60', minimumBranchCoverage: '40', minimumClassCoverage: '50', minimumComplexityCoverage: '40', minimumInstructionCoverage: '40', minimumLineCoverage: '40', minimumMethodCoverage: '40'
                findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/findbugs/main.xml', unHealthy: ''
            }
        }
    }
}