#!/usr/bin/env groovy
pipeline {
    agent any

    stages {
        stage('Clean') {
            steps {
                sh 'gradle clean'
                dir('client') {
                    dir('node_modules') {
                        deleteDir()
                    }
                    sh 'npm install'
                }
            }
        }        
        stage('Build') {
            steps {
                sh 'gradle build'
                dir('client') {
                    sh 'npm run-script build'
                    sh 'npm run-script test -- --single-run --code-coverage --browsers ChromeHeadless'
                }
            }
        }
        stage('Static code analysis') {
            steps {
                sh 'gradle pmd pmdMain'
                sh 'gradle checkstyle checkstyleMain'
                sh 'gradle findbugs findbugsMain'
                dir('client') {
                    sh "mkdir checkstyle"
                    sh 'npm run-script lint -- --format checkstyle --force true | awk "{if(NR > 3) print $0}" > checkstyle/main.xml'
                }
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
                checkstyle canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/checkstyle/main.xml', unHealthy: ''
                pmd canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/pmd/main.xml', unHealthy: ''
                openTasks canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', high: 'FIXME,HACK', ignoreCase: true, low: 'NOTE', normal: 'TODO', pattern: '**/*java,client/src/**/*.ts', unHealthy: ''
                dry canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/build/**/cpd/cpdCheck.xml', unHealthy: ''
            }
        }
    }
}