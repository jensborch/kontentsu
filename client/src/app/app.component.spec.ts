/* tslint:disable:no-unused-variable */

import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { TemplateComponent } from './template.component';
import { ContentService } from './content.service';
import { Title } from '@angular/platform-browser';

class MockContentService {

  getPage(path: String): Promise<any> {
    return new Promise((resolve, reject) => {
      resolve({
        "content": { "heading": "Test title" }
      });
    });
  }
}

describe('App: Kontentsu', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        TemplateComponent
      ],
      imports: [

      ],
      providers: [
        { provide: ContentService, useClass: MockContentService },
        { provide: Title, useClass: Title }
      ]
    }).compileComponents();
  }));

  it('should create the app', async(() => {
    let fixture = TestBed.createComponent(AppComponent);
    let app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  /*it(`should have title`, async(() => {
    let fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    fixture.detectChanges();
    let titleService = TestBed.get(Title);
    expect(titleService.getTitle()).toEqual('Test title');
  }));*/

  it('should render title in a h1 tag', async(() => {
    let fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    let compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Kontentsu editing application');
  }));
});
