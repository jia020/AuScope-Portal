import { Component, OnInit } from '@angular/core';
import {OlMapObject} from '../../portal-core-ui/service/openlayermap/ol-map-object';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-olmap-baselayerselector',
  templateUrl: './olmap.baselayerselector.component.html',
  styleUrls: ['./olmap.baselayerselector.component.css']

})
export class OlmapBaselayerselectorComponent implements OnInit {
  public selectedLayer = 'Road';
  baseMapLayers: any = [];
  constructor(public olMapObject: OlMapObject) {
   }

  ngOnInit() {
    this.baseMapLayers = environment.baseMapLayers;
    this.updateBaseMap('Road');
  }

  public updateBaseMap(selected: string) {
    this.olMapObject.switchBaseMap(selected);
  }

}
