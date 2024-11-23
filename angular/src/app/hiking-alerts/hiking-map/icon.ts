import {divIcon, DivIcon, Point} from 'leaflet';

export class Icon {
  mainIcon: DivIcon;

  constructor(leaflet: any) {
    const iconSize = new Point(36,36);
    this.mainIcon = leaflet.divIcon({
      className: 'custom-marker',
      html: '<div style="background-color: #cc3939d9; border: 5px solid transparent; font-weight: bold; font-size: 36px; display: flex; justify-content: center; align-items: center;  color: white; border-radius: 50%; width: 36px; height: 36px; opacity: 0.95;">!</div>',
      iconSize: iconSize,
      iconAnchor: [18, 18],
    });
  }

  getMainIcon(): DivIcon {
    return this.mainIcon;
  }

  getClusterIcon(count: any): DivIcon {
    var pix = 36;
    if (count > 1000){
      pix = 72;
    } else if (count > 500){
      pix = 56;
    } else if (count > 100){
      pix = 48;
    } else if (count > 50){
      pix = 44;
    }

    return divIcon({
      html: '<div style="background-color: #cc3939d9; color: white; display: flex; justify-content: center; align-items: center; opacity: 0.95; font-size: 20px; width: ' + pix + 'px; height: ' + pix + 'px; border-radius: 50%;"><span>' + count + '</span></div>',
      className: 'marker-cluster',
      iconSize: [40, 40]
    });
  }
}


