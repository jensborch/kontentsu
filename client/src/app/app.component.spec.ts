/* tslint:disable:no-unused-variable */

import { Component } from '@angular/core';
import { TestBed, async, inject } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { TemplateComponent } from './template/template.component';
import { Page } from './page';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { Observable } from "rxjs/Rx";
import { DOCUMENT } from '@angular/platform-browser';

class MockContentService {

  load(url?: string) {
  };

  getPage(): Observable<Page> {
    return Observable.of(new Page(
      "template",
      {
        content: { heading: 'Test title' }
      }));
  }
}

@Component({
  template: '',
  selector: 'k-template'
})
class MockTemplateComponent { };

const contentService: MockContentService = new MockContentService();

describe('App: Kontentsu', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        { provide: ContentService, useValue: contentService },
        Logger
      ],
      declarations: [AppComponent, MockTemplateComponent]
    })
      .compileComponents();
  }));

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
