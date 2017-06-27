import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpModule, XHRBackend } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { TemplateComponent } from './template.component';
import { ContentService } from './../content.service';
import { Logger } from '../logger.service';

describe('TemplateComponent', () => {
  let component: TemplateComponent;
  let fixture: ComponentFixture<TemplateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      declarations: [TemplateComponent],
      providers: [
        { provide: XHRBackend, useClass: MockBackend },
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
