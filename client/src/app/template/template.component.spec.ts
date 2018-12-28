import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {
  HttpClientTestingModule,
} from "@angular/common/http/testing";
import { TemplateComponent } from './template.component';
import { ContentService } from '../content/content.service';
import { Logger } from '../logger/logger.service';
import { Compiler } from '@angular/core';

describe('Component: Template', () => {
  let component: TemplateComponent;
  let fixture: ComponentFixture<TemplateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [TemplateComponent],
      providers: [
        Compiler,
        ContentService,
        Logger
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
