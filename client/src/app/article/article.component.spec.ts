import { TestBed } from '@angular/core/testing';
import { ArticleComponent } from './article.component';
import { Directive, Input, Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { ComponentFixture } from '@angular/core/testing';

@Component({
  selector: 'k-content',
  template: ''
})
class MockContentComponent {
  @Input('data')
  public data: any;

}

describe('Component: Article', () => {

  let fixture: ComponentFixture<ArticleComponent>;
  let comp: ArticleComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ArticleComponent, MockContentComponent]
    });
    fixture = TestBed.createComponent(ArticleComponent);
    comp = fixture.componentInstance;
  });
  it('should create an instance', () => {
    expect(comp).not.toBeNull();
  });
  it('should be empty with no data', () => {
    const a = fixture.debugElement.query(By.css('article'));
    expect(a).toBeNull();
  });
  it('should have article and paragraph content', () => {
    comp.data = {
      sections: [
        {
          paragraphs: [
            'test'
          ]
        }
      ]
    };
    fixture.detectChanges();
    const a = fixture.debugElement.query(By.css('article'));
    expect(a).not.toBeNull();
    const p = fixture.debugElement.query(By.css('p'));
    expect(p).not.toBeNull();
    const c = fixture.debugElement.query(By.directive(MockContentComponent));
    expect(c).not.toBeNull();
    expect(c.componentInstance.data).toContain('test');
  });
});
