import { TestBed } from '@angular/core/testing';
import { ArticleComponent } from './article.component';
import { Directive, Input, Component } from '@angular/core';

@Component({
  selector: 'k-content',
  template: ''
})
class MockContentDirective {
  @Input('data')
  public data: any;

}

describe('Component: Article', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ArticleComponent, MockContentDirective]
    });
  });
  it('should create an instance', () => {
    const fixture = TestBed.createComponent(ArticleComponent);
    const comp = fixture.componentInstance;
    expect(comp).not.toBeNull();
  });
});
