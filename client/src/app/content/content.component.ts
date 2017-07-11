import {
  Component,
  Input,
  OnInit,
  ViewChild, ViewContainerRef, ComponentRef,
  Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver
} from '@angular/core';
import { PageModule } from '../page.module';

@Component({
  selector: 'k-content',
  template: '<ng-template #content></ng-template>'
})
export class ContentComponent implements OnInit {

  @Input() data: string;

  @ViewChild('content', { read: ViewContainerRef })
  content: ViewContainerRef;

  constructor(private compiler: Compiler) { }

  ngOnInit() {
    const factory = this.createComponentFactory(this.data);
    this.content.createComponent(factory);
  }

  private createComponentFactory(data: string): ComponentFactory<any> {

    @Component({
      selector: 'k-dynamic-content',
      template: data
    })
    class DynamicContentComponent {
    }

    @NgModule({
      imports: [PageModule],
      declarations: [DynamicContentComponent]
    })
    class DynamicContentModule { }

    const module: ModuleWithComponentFactories<any> = this.compiler.compileModuleAndAllComponentsSync(DynamicContentModule);
    return module.componentFactories.find(f => f.componentType === DynamicContentComponent);
  }

}
