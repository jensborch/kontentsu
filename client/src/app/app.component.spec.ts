/* tslint:disable:no-unused-variable */

import { Component } from '@angular/core';
import { TestBed, async  } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { Page } from './page';
import { ContentService } from './content/content.service';
import { Logger } from './logger/logger.service';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of } from 'rxjs';

class MockContentService {

  load(url?: string) {
  };

  getPage(): Observable<Page> {
    return of(new Page(
      'template',
      {
        content: { heading: 'Test title' }
      }));
  }
}

@Component({
  selector: 'k-template',
  template: ''
})
class MockTemplateComponent { };

const contentService: MockContentService = new MockContentService();

describe('App: Kontentsu', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([])
      ],
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
