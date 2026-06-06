package br.edu.ifpe.dnc.parser;

import br.edu.ifpe.dnc.dto.network.*;
import com.google.gson.Gson;
import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.ethernet.plca.PLCAWeightWRR;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig.*;
import org.networkcalculus.dnc.ethernet.tsn.data.DatasetReader;
import org.networkcalculus.dnc.ethernet.tsn.dto.*;
import org.networkcalculus.dnc.ethernet.tsn.model.GCLScheduleEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.tsn_interface.EthernetPCP;
import org.networkcalculus.dnc.ethernet.tsn.model.tsn_interface.EthernetPHYStandard;
import org.networkcalculus.dnc.ethernet.tsn.results.database.paper_access_2018.DatabaseResults2PaperAccess2018;
import org.networkcalculus.dnc.tandem.TandemAnalysis.Analyses;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkJsonParser {

    private static final Gson gson = new Gson();

    public static EthernetNetworkDTO parse(String jsonContent) {
        NetworkDTO dto = gson.fromJson(jsonContent, NetworkDTO.class);

        ExecutionConfig executionConfig = buildExecutionConfig(dto.getExecutionConfig());

        Map<String, Object> metadata = new LinkedHashMap<>();
        if (dto.getNetworkCase() != null) {
            metadata.put(DatasetReader.DATASET_METADATA_NETWORK_CASE,
                    DatabaseResults2PaperAccess2018.NetworkCase.parse(dto.getNetworkCase()));
        }
        if (dto.getDatasetName() != null) {
            metadata.put(DatasetReader.DATASET_METADATA_DATASET_CASE,
                    DatabaseResults2PaperAccess2018.DatasetCase.parse(dto.getDatasetName()));
        }
        if (dto.getDescription() != null) {
            metadata.put(DatasetReader.DATASET_METADATA_DATASET_DESCRIPTION, dto.getDescription());
        }

        EthernetNetworkDTO networkDTO = new EthernetNetworkDTO(metadata, executionConfig);

        mapDevices(dto, networkDTO);
        mapFlows(dto, networkDTO);

        return networkDTO;
    }

    private static ExecutionConfig buildExecutionConfig(ExecutionConfigDTO dto) {
        if (dto == null) throw new IllegalArgumentException("executionConfig is required");

        Set<Analyses> analyses = (dto.getAnalyses() != null && !dto.getAnalyses().isEmpty())
                ? dto.getAnalyses().stream().map(Analyses::valueOf).collect(Collectors.toSet())
                : Set.of(Analyses.TFA, Analyses.SFA);

        String multiplexing = dto.getMultiplexing() != null ? dto.getMultiplexing() : "FIFO";

        return new ExecutionConfig(
                PrioritizingOrder.valueOf(dto.getPrioritizingOrder()),
                PLCAModeling.valueOf(dto.getPlcaModeling()),
                FrameSizeSchedulingValidationOption.valueOf(dto.getValidateSchedulingForFrameSize()),
                AnalysisConfig.Multiplexing.valueOf(multiplexing),
                analyses
        );
    }

    private static void mapDevices(NetworkDTO dto, EthernetNetworkDTO networkDTO) {
        for (DeviceDTO dDto : dto.getDevices()) {
            Map<Integer, EthernetInterfaceDTO> interfaces = new LinkedHashMap<>();

            for (InterfaceDTO iDto : dDto.getInterfaces()) {
                EthernetPHYStandard phyStandard = EthernetPHYStandard.findByName(iDto.getPhyStandard());
                PLCAWeightWRR plcaWeight = iDto.getPlcaWeightWRR() > 0
                        ? new PLCAWeightWRR(iDto.getPlcaWeightWRR())
                        : null;

                Map<EthernetPCP, List<GCLScheduleEntry>> tasSchedule = new HashMap<>();

                if (iDto.getSchedule() != null) {
                    for (ScheduleEntryDTO sDto : iDto.getSchedule()) {
                        EthernetPCP pcp = new EthernetPCP(sDto.getPriority());
                        GCLScheduleEntry gcl = new GCLScheduleEntry(
                                sDto.getOpenTime(), sDto.getCloseTime(), sDto.getPeriodLength(), pcp);
                        tasSchedule.computeIfAbsent(pcp, k -> new ArrayList<>()).add(gcl);
                    }
                }

                EthernetInterfaceDTO interfaceDTO = new EthernetInterfaceDTO(
                        iDto.getInterfaceId(), phyStandard, plcaWeight, tasSchedule);
                interfaces.put(interfaceDTO.interfaceId(), interfaceDTO);
            }

            EthernetDeviceDTO deviceDTO = new EthernetDeviceDTO(
                    dDto.getDeviceName(), (double) dDto.getProcessingDelay(), interfaces);
            networkDTO.deviceDTOMap().put(deviceDTO.deviceName(), deviceDTO);
        }
    }

    private static void mapFlows(NetworkDTO dto, EthernetNetworkDTO networkDTO) {
        for (FlowDTO fDto : dto.getFlows()) {
            String pathId = "vl_" + fDto.getFlowName();

            List<LinkHopDTO> hops = new ArrayList<>();
            for (PathHopDTO hop : fDto.getPath()) {
                hops.add(new LinkHopDTO(
                        DeviceAndInterfaceDTO.parse(hop.getSrc()),
                        DeviceAndInterfaceDTO.parse(hop.getDst())
                ));
            }

            VirtualFullPathDTO pathDTO = new VirtualFullPathDTO(pathId, hops);
            networkDTO.virtualFullPathDTOMap().put(pathId, pathDTO);

            EthernetPCP priorityPCP = new EthernetPCP(fDto.getPriority());
            STMessageDTO messageDTO = new STMessageDTO(
                    fDto.getFlowName(), fDto.getSizeBytes(), fDto.getDeadlineUs(),
                    pathId, fDto.getType(), priorityPCP, fDto.getPeriodUs(), fDto.getOffsetUs()
            );

            EthernetDeviceDTO sourceDevice = networkDTO.deviceDTOMap().get(fDto.getSourceDevice());
            EthernetDeviceDTO destDevice = networkDTO.deviceDTOMap().get(fDto.getDestinationDevice());

            STFlowDTO flowDTO = new STFlowDTO(messageDTO, sourceDevice, destDevice, pathDTO);
            networkDTO.stFlowDTOMap().put(fDto.getFlowName(), flowDTO);
        }
    }
}
