import styles from "./List.module.css";
import Filter from "../../components/Fliter/Filter";
import ListSearch from "../../components/ListSearch/ListSearch";
import {data} from "./../../const/data";
import { ListBox } from './../../components/ListBox/ListBox';


const List = () => (
  <>
    <div className={styles["list-component-container"]}>
      <Filter />
      <ListSearch />
    </div>
    <section className={styles["list-list-container"]}>
      <div  className={styles["list-list-box-container"]}>
    {data.map((el) => {
        return <ListBox key={el.id} title={el.title} like={el.like} jjim={el.jjim} runTime={el.runTime} intro={el.intro} addressBrief={el.addressBrief}  img1={el.src1} img2={el.src2}/>;
      })}
      </div>
    </section>
  </>
);

export default List;

