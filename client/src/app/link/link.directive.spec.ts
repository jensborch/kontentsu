import { LinkDirective } from './link.directive';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';

@Component({
  selector: 'k-test-component',
  template: '<a href="test"></a>'
})
class TestComponent { }

describe('LinkDirective', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([])
      ],
      providers: [],
      declarations: [TestComponent, LinkDirective]
    });
  });
  it('should create an instance', () => {
    const fixture = TestBed.createComponent(TestComponent);
    const directiveEl = fixture.debugElement.query(By.directive(LinkDirective));
    expect(directiveEl).not.toBeNull();
  });
});
