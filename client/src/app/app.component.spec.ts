/* tslint:disable:no-unused-variable */

import { HttpModule, Http } from '@angular/http';
import { TestBed, async, inject } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { TemplateComponent } from './template/template.component';
import { ContentService } from './content.service';
import { Logger } from './logger.service';

class MockContentService {

  getPage(path: String): Promise<any> {
    return new Promise((resolve, reject) => {
      resolve({
        content: { heading: 'Test title' }
      });
    });
  }
}

class MockHttp {

  get(url: String) {
    return {
      toPromise: () => {
        return new Promise((resolve, reject) => {
          resolve({
            text: () => '<h1>{{page.content.heading}}</h1>'
          });
        });
      }
    };
  }
}

describe('App: Kontentsu', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        { provide: Http, useClass: MockHttp },
        { provide: ContentService, useClass: MockContentService },
        { provide: Title, useClass: Title },
        Logger
      ],
      declarations: [
        AppComponent,
        TemplateComponent
      ]
    }).compileComponents();
  }));

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  it('should have title', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        const titleService = TestBed.get(Title);
        expect(titleService.getTitle()).toEqual('Test title');
      });
    });
  }));

  it('should render title in a h1 tag', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const compiled = fixture.debugElement.nativeElement;
      expect(compiled.querySelector('h1').textContent).toContain('Test title');
    });
  }));
});
